//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.main;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.StringTokenizer;

/**
 * <p>Spiralcraft application mode launcher, for running Spiralcraft
 *   applications
 *   associated with the filesystem such as "installed" applications, 
 *   servers, and command line tools.
 * </p>
 * 
 * <p>This "main" class bootstraps the ApplicationManager in spiralcraft-core
 *   into
 *   its own classloader. The ApplicationManager will then load individual 
 *   applications into their own classloaders, in order to isolate applications
 *   from each other and from the management system.
 * </p>
 * 
 */
public class Spiralcraft
{

  // Debug flag for use by loader infrastructure
  public static boolean DEBUG=false;
  public static PrintStream err=System.err;
  public static boolean GUI_REQUESTED;
  public static URI EXECUTION_CONTEXT_URI;
  public static URI GUI_EXECUTION_CONTEXT_URI
    =URI.create("class:/spiralcraft/launcher/RootGuiConsole");
  public static String defaultlaf
    ="com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";

  private String _spiralcraftHome=null;
  private String _codebase=null;
  
  
  private String logFile=null;
  private String lockFile=null;
  @SuppressWarnings("unused")
  private FileLock lock;
  
  static
  {
    if (System.getProperty("swing.defaultlaf")==null)
    { System.setProperty("swing.defaultlaf",defaultlaf);
    }
    
    
    // Fix broken JAR caching mechanism by disabling URLConnection caching
    try 
    {
      URL url = new URL("jar:file://dummy.jar!/");
      URLConnection uConn = url.openConnection();
      uConn.setDefaultUseCaches(false);
    } 
    catch (MalformedURLException e) 
    {
    } 
    catch (IOException e) 
    {
    }    
  }
  
  /**
   * Instantiate a new instance and delegate to instance main
   */
  public static void main(String[] args)
  { 
    int ret=new Spiralcraft().run(args);
    if (ret!=0)
    { System.exit(ret);
    }
  }

  /**
   * Process arguments and run the application loader
   */
  protected int run(String[] args)
  {
    String[] delegateArgs=processArguments(args);
    
    initializeEnvironment();

    return runLoader(delegateArgs);
  }

  /**
   * Extract known arguments from the argument list
   */
  private String[] processArguments(String[] args)
  {
    LinkedList<String> extraArgs=new LinkedList<String>();
    String[] delegateArgs=null;

    for (int i=0;i<args.length;i++)
    {
      if (args[i].startsWith("-"))
      {
        String option=args[i].substring(1).intern();
        if (option=="-debug")
        { 
          DEBUG=true;
          debug("Spiralcraft boot debugging activated");
          
        }
        else if (option=="-spiralcraft.home")
        { _spiralcraftHome=args[++i];
        }
        else if (option=="-codebase")
        { _codebase=args[++i];
        }
        else if (option=="-log")
        { logFile=args[++i];
        }
        else if (option=="-gui")
        { 
          GUI_REQUESTED=true;
          EXECUTION_CONTEXT_URI=GUI_EXECUTION_CONTEXT_URI;
        }
        else if (option=="-lock")
        { lockFile=args[++i];
        }
        else
        { extraArgs.add(args[i]);
        }
      }
      else
      { 
        delegateArgs=new String[extraArgs.size()+args.length-i];
        extraArgs.toArray(delegateArgs);
        System.arraycopy
          (args
          ,i
          ,delegateArgs
          ,extraArgs.size()
          ,delegateArgs.length-extraArgs.size()
          );
        return delegateArgs;
      } 
    }
    return new String[0];
  }

  private void checkLock()
  {
    try
    {
      if (lockFile!=null)
      {
        Path lockPath=FileSystems.getDefault().getPath(lockFile,new String[0]);
        Files.deleteIfExists(lockPath);
        FileChannel lockChan=FileChannel.open
            (lockPath
            ,new OpenOption[] 
                {StandardOpenOption.CREATE
                ,StandardOpenOption.WRITE
                ,StandardOpenOption.READ
                ,StandardOpenOption.SYNC
                ,StandardOpenOption.DELETE_ON_CLOSE
                }
            );
        lock=lockChan.lock(0L,Long.MAX_VALUE,true);
      }
    }
    catch (IOException x)
    { throw new IllegalStateException("Could not lock "+lockFile,x);
    }
    if (DEBUG)
    { debug("Locked file "+lockFile);
    }
  }
  
  /**
   * Create standard system properties from known information
   */
  private void initializeEnvironment()
  {
    checkLock();
    
    if (logFile!=null)
    { 
      try
      { err=new PrintStream(new FileOutputStream(logFile));
      }
      catch (IOException x)
      { throw new IllegalArgumentException(logFile,x);
      }
    }
    
    if (System.getProperty("spiralcraft.home")==null)
    {
      if (_spiralcraftHome==null)
      { 
        if (DEBUG)
        { debug("Determining spiralcraft.home from classpath");
        }
        _spiralcraftHome=readHomeFromClasspath();
      }
      
      if (_spiralcraftHome==null)
      { _spiralcraftHome=".";
      }
      
      if (_spiralcraftHome!=null)
      { 
        System.setProperty("spiralcraft.home",new File(_spiralcraftHome).getAbsolutePath());
      }
    }
    
    if (DEBUG)
    { debug("spiralcraft.home="+System.getProperty("spiralcraft.home"));
    }
    _spiralcraftHome=System.getProperty("spiralcraft.home");
  }

  /**
   * Try to determine the home directory from the classpath
   */
  private String readHomeFromClasspath()
  {
    ArrayList<String> pathElementList=new ArrayList<String>();
    String classPath=System.getProperty("java.class.path");
    if (DEBUG)
    { debug("java.class.path="+classPath);
    }
    if (classPath!=null)
    { 
      StringTokenizer pathTok=new StringTokenizer(classPath,File.pathSeparator,false);
      while (pathTok.hasMoreTokens())
      { pathElementList.add(pathTok.nextToken());
      }
      
      String[] pathElements=new String[pathElementList.size()];
      pathElementList.toArray(pathElements);

      for (String pathElement : pathElements)
      {
        if (pathElement.endsWith("spiralcraft.jar"))
        { 
          File jarFile=new File(pathElement);
          jarFile=new File(jarFile.getAbsolutePath());
          String parent=jarFile.getParent();
          if (parent.endsWith("lib"))
          { return new File(parent).getParent();
          }
        }
      }
    }
    return null;
  }

  /**
   * Instantiate a classloader for the Spiralcraft core module and
   *   use it to load the LoaderDelegate. Run the LoaderDelegate
   *   with the appropriate arguments
   */
  private int runLoader(String[] args)
  { 
    Class<?> mainClass=null;
    LauncherClassLoader classLoader=new LauncherClassLoader();
    try
    {
      if (_spiralcraftHome!=null)
      { 
        if (DEBUG)
        { 
          debug("Creating JarClassLoader for "
                +_spiralcraftHome+File.separator
                +"lib/spiralcraft-core.jar"
                );
        }
        
        classLoader.addResource
          (new JarClassResource
            (_spiralcraftHome+File.separator+"lib/spiralcraft-core.jar"));
        classLoader.addResource
          (new JarClassResource
            (_spiralcraftHome+File.separator+"lib/spiralcraft-launcher.jar"));
        if (GUI_REQUESTED)
        {
          classLoader.addResource
            (new JarClassResource
              (_spiralcraftHome+File.separator+"lib/spiralcraft-gui.jar"));
//          classLoader.addResource
//            (new JarClassResource
//              (_spiralcraftHome+File.separator+"lib/spiralcraft-shell.jar"));
        }
      }
      else
      { throw new InstantiationException("Spiralcraft home could not be found.");
      }
      
      if (_codebase!=null)
      { System.setProperty("spiralcraft.codebase",_codebase);
      }

      mainClass=classLoader.loadClass("spiralcraft.launcher.Main");
      
      if (DEBUG)
      { debug("Loaded main class");
      }

      if (mainClass.getClassLoader()==Spiralcraft.class.getClassLoader())
      { 
        debug
          ("WARNING: module spiralcraft-main did not expect to find module"
          +" spiralcraft-core in its classloader. This may interfere with"
          +" dynamic module loading and may cause some application to"
          +" malfunction. Removal of spiralcraft-core and other modules from"
          +" the Java classpath is recommended."
          );
      }
    }
    catch (Exception x)
    { x.printStackTrace();
    }

    if (mainClass!=null)
    { 
      if (DEBUG)
      { debug("Running main class");
      }
      try
      {
        Method mainMethod=
          mainClass.getMethod("exec",new Class<?>[] {String[].class});
        if (mainMethod!=null)
        { 
          ClassLoader oldLoader=Thread.currentThread().getContextClassLoader();
          Class<?> disposableContext
            =classLoader.loadClass("spiralcraft.common.DisposableContext");
          try
          { 
            Thread.currentThread().setContextClassLoader(classLoader);
            disposableContext.getMethod("push").invoke(null);
            mainMethod.invoke(null,new Object[] {args});
          }
          finally
          { 
            disposableContext.getMethod("pop").invoke(null);
            disposableContext=null;
            Thread.currentThread().setContextClassLoader(oldLoader);
          }
        }
        mainMethod=null;
        return 0;
      }
      catch (InvocationTargetException x)
      { 
        while (x.getTargetException() instanceof InvocationTargetException)
        { x=(InvocationTargetException) x.getTargetException();
        }
        x.getTargetException().printStackTrace();
        return 1;
      }
      catch (Exception x)
      { 
        x.printStackTrace();
        return 1;
      }
      finally
      { 
        classLoader.shutdown();
        mainClass=null;
      }
    }
    else
    { 
      classLoader.shutdown();
      mainClass=null;
      return 1;
    }
    
  }
  
  private void debug(String msg)
  { err.println(msg);
  }

}

