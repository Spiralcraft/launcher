//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.loader;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import spiralcraft.util.Arguments;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.string.StringUtil;



/**
 * <P>Provides necessary context for running application code. 
 * </P>
 * 
 * <P>Specifically, the ApplicationEnvironment creates a ClassLoader which
 *   references an appropriate set of code libraries, and resolves and executes
 *   a suitable entry point.
 * </P>
 * 
 * <P>The standard and default Spiralcraft entry point is the 
 *  spiralcraft.exec.Executor class, which loads and executes an application
 *  specified by a URI.
 * </P>
 * 
 * <P>An alternate class with the Java standard "main" method can be invoked
 *   using the "-main &lt;classname&gt;" option.
 * </P>
 * 
 * <P>The "-exec &lt;classname&gt;" option invokes the 
 *   spiralcraft.exec.ClassExecutor entry point, which accepts a class name
 *   as an argument. The specified class must implement 
 *   the spiralcraft.exec.Executable interface.
 * </P>
 * 
 * <P>The "-module" option specifies one or more code libraries (.jar names or
 *   directories containing code) which will be available to the target
 *   application ClassLoader. 
 * </P>
 * 
 * <P>An ApplicationEnvironment must be associated with an ApplicationManager,
 *   which provides access to the available codebase.
 * </P>
 *  
 * <P>Note: The ClassLoader created by the ApplicationEnvironment is constructed
 *   so that it's parent is the Java system application classloader. 
 *   This preserves forward compatability by allowing the target
 *   application to use different versions of the libraries which compose this
 *   "boot" loading system. 
 * </P>
 */
public class ApplicationEnvironment
{
  private LibraryClassLoader _classLoader;
  // private ApplicationManager _applicationManager;
  private String _mainClass;
  private String _mainMethodName;
  private String[] _mainArguments=new String[0];
  private String[] _commandLineArguments=new String[0];
  private String[] _modules;
  private boolean debug;

  /**
   * The ApplicationManager provides access to the entire installed
   *   codebase.
   */
  void resolve(ApplicationManager manager)
  { 
    // _applicationManager=manager;
    _classLoader=new LibraryClassLoader(manager.getLibraryCatalog());
    _classLoader.setDebug(debug);
  }

  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  /**
   * Standard arguments to the main class, as a String to be tokenized.
   *   These arguments will preceed any arguments passed to the exec() method.
   */
  public void setCommandLine(String val)
  { _commandLineArguments=StringUtil.tokenizeCommandLine(val);
  }
  
  /**
   * The main class, to run. This will be overridden if the main class is
   *   specified as an option in the arguments passed to the exec() method
   */
  public void setMainClass(String val)
  { _mainClass=val;
  }
  
  /**
   * <p>The name of the "main" method. Any static method with a single String[]
   *   parameter can be used as the entry point.
   * </p>
   * @param val
   */
  public void setMainMethodName(String val)
  { _mainMethodName=val;
  }

  /**
   * Standard arguments to the main class.
   *   These arguments will preceed any arguments passed to the exec() method.
   */
  public void setMainArguments(String[] val)
  { 
    if (val==null || val.length==0)
    { new Exception().printStackTrace();
    }
    _mainArguments=val;
  }
  
  /**
   * A list of additional modules to load. These modules will normally contain
   *   classes that are to be dynamically loaded but cannot be resolved
   *   automatically by the LibraryClassloader.
   */
  public void setModules(String[] val)
  { _modules=val;
  }
  
  
  /**
   * Load the codebase and execute a command. 
   */
  public void exec(String[] args)
    throws ExecutionTargetException
  { 
    processArguments(args);

    if (_mainClass==null)
    { 
      _mainClass="spiralcraft.exec.Executor";
      _mainMethodName="launch";
    }

    try  
    {
      if (_modules!=null)
      {
        for (int i=0;i<_modules.length;i++)
        { _classLoader.addModule(_modules[i]);
        }
      }
      else
      { 
        _classLoader.addAllModules();
        
        // This is unreliable- we might need something we can't reach
        //   from the main class.
        
        // _classLoader.resolveLibrariesForClass(_mainClass);
      }
  
      Class<?> clazz=_classLoader.loadClass(_mainClass);
      Method mainMethod=clazz.getMethod(_mainMethodName,new Class[] {String[].class});
      
      ClassLoader oldLoader=Thread.currentThread().getContextClassLoader();
      try
      {
        Thread.currentThread().setContextClassLoader(_classLoader);
        mainMethod.invoke(null,new Object[] {_mainArguments});
      }
      finally
      { Thread.currentThread().setContextClassLoader(oldLoader);
      }
    }
    catch (InvocationTargetException x)
    { 
      while (x.getTargetException() instanceof InvocationTargetException)
      { x=(InvocationTargetException) x.getTargetException();
      }
      throw new ExecutionTargetException(x.getTargetException());
    }
    catch (Exception x)
    { throw new ExecutionTargetException(x);
    }
      
  }

  /**
   * Process arguments. Any options specified
   */
  private void processArguments(String[] args)
  {
    _mainArguments
      =(String[]) ArrayUtil.appendArrays(_mainArguments,_commandLineArguments);
    new Arguments()
    {
      @Override
      public boolean processArgument(String argument)
      { 
        if (_mainClass==null)
        { 
          _mainClass="spiralcraft.exec.Executor";
          _mainMethodName="launch";
        }
        _mainArguments=(String[]) ArrayUtil.append(_mainArguments,argument);
        return true;
      }

      @Override
      public boolean processOption(String option)
      { 
        if (_mainClass==null)
        {
          if (option=="module")
          { _modules=(String[]) ArrayUtil.append(_modules,nextArgument());
          }
          else if (option=="main")
          { 
            _mainClass=nextArgument();
            //XXX Figure out what to do if null
            if (_mainMethodName==null)
            { _mainMethodName="main";
            }
          }
          else if (option=="exec")
          { _mainClass="spiralcraft.exec.ClassExecutor";
          }
          else
          { return false;
          }
        }
        else
        { _mainArguments=(String[]) ArrayUtil.append(_mainArguments,"-"+option);
        }
        return true;
      }

    }.process(args,'-');
  }    
  
}

/* Notes
 
2007-11-14

For the most part, we're going to use the ApplicationEnvironment to setup a
  ClassLoader and call the spiralcraft.exec.Executor.main(String[] args).
  
Functionality will be retained to call other 'main' classes in a variety of
  ways so as to interface cleanly with non-Spiralcraft 'main' entry points,
  but environment should be set up in these cases to provide Spiralcraft
  functionality reachable from non-Spiralcraft entry points with a standard
  environment.

2003-02-??

This class is basically responsible for setting up an appropriate classloader
and then running an entry point in code loaded from that classloader.

Currently, all configuration options are specified externally through
  "persistent object" files which represent a given environment config.
  
Typically, each module provides an environment file to make it easy to run
  entry points in the module from 'outside'. 
  
The entry point is assumed to be a 'main' method. This is the simplest way
  of doing things, but by design does not solve the problem of providing
  much context for execution as a true shell environment would. As a standard
  default, using the main method leaves much to be desired. THERE IS A
  COMPETING GOAL HERE, WHICH IS TO STANDARDIZE ON A MORE FUNCTIONAL ENTRY
  POINT.
  
  Due to classloader separation, we can't share interfaces beyond the 
  Java standard class libraries. But the highlight of a more functional entry
  point is a useful IO system, which seems almost impossible to accomplish
  without having the client refer to classes not loaded in its loader set, and
  would be difficult if the server can only pass opaque objects to the client.
  
  On the other hand, a 'useful IO system' can go to the extent of the Command
    framework, which is a much more complex system. So we need to revisit what
    exactly we need to build here.
    
  The immediate requirement is driven from wanting to create invocable
    functionality that can be accessed from a simple command line. Currently,
    it goes like this:
      1. Write a class with a 'main' method
      2. Make sure there is an environment definition set up for the module
      3. Invoke "spiralcraft <env> <className>"
      
  Problems with the above: No opportunity for IO redirection- this means that
    we can't use our classes from anything but a command line to output to
    a command line. We don't have a good concept of a URI context for relative
    path resolution either. We just have the static File(".") user current 
    directory.
    
  General solution: We need to be able to code at least the client side of
    executables with a more robust sense of context. Specifically, redirection
    of standard streams and contextualization for resources.
    
  An ExecutionContext might be a good compromise- it can have standard IO
    stream management and provide a "user.dir" type of property. This is
    essentially a VirtualSystem class. Maybe we should call it that. 
  
  Observation- these enhancements fall under the category of abstracting what
    is normally provided to a main() method by making it less environment
    specific- ie. generic streams instead of global system out,in,err, and a
    URI based resource context instead of a file based context. It does not
    add any functionality.
    
  The assumption of the ExecutionContext is that we are really building a
    rudimentary user interface mechanism, and we want to stick with universally
    correct assumptions about the basic things that we need to handle. One by
    one:
    
    System.out: 
      This is primarily used to send output. No assumptions are made
      about the output format here- that is application specific. The 
      destination should be managed by the context, however.
    System.err: 
      This is primarily used to send error or control information.
      Again, the format is application specific, depending on the ultimate
      receiver. Most of the time, formatted user messages go through this 
      channel, but it seems like this would be application specific as well.
      The destination should be managed by the context, however.
    System.in:
      This is used to read input. Once again, application specific. The source
      should be managed by the context, however.
    System.getProperty("user.dir"):
      This is used as a context for finding resources when relative paths
      are specified on the command line. The relevent URI resolution system
      should be determined by the context.
    Arguments/Parameters:
      A String[] is minimally functional. Parameters are essentially pointers
        to more information. Would an Object[] be better? A user can enter
        parameters on a command line. Commands can be stored as text in code.
        It might be presumptive to enforce a standard interpretation of 
        command line arguments. We don't need to do this. If we want to
        pass in named objects, perhaps a better way is to have the 'invoker'
        configure things itself using beans and leave the parameters alone.
    Environment:
      "Global" properties need to have some control of scope. We need to provide
      some kind of dictionary, again, with Strings. Arguments are preferred, but
      'properties' are a mechanism that stays the same from execution
      to execution.
    
  
  




*/
