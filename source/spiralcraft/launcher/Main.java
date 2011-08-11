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
package spiralcraft.launcher;

import spiralcraft.common.ContextualException;
import spiralcraft.data.persist.AbstractXmlObject;
import spiralcraft.exec.ExecutionContext;
import spiralcraft.main.Spiralcraft;

import spiralcraft.lang.reflect.BeanFocus;

import spiralcraft.util.ArrayUtil;

import spiralcraft.vfs.StreamUtil;
import spiralcraft.vfs.context.ContextResourceMap;

import java.io.Console;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


import java.net.URI;
//import java.util.Map;


/**
 * <p>Standard entry point for launching managed applications. 
 * </p>
 * 
 * <p>This class sets up a "global" ApplicationManager and gives it control
 *   of the process.
 * </p>
 * 
 * <p>Note: In order for the ApplicationManager to run properly, this class
 *   MUST NOT be loaded into the system classloader (ie. called directly from
 *   the OS), as this will prevent applications which use different versions
 *   of the Spiralcraft core module and other shared modules from loading
 *   classes. 
 *  
 *   This class designed to be loaded in a classloader other than the System
 *   classloader, for example, in the classloader created by the
 *   spiralcraft-main package by invoking the spiralcraft.main.Spiralcraft
 *   class (the 'bootstrap loader') from the command line.
 *
 *   If the ClassLoader for this class is the System classLoader, an exception
 *   will be thrown on instantiation.
 * </p>
 */
public class Main
{
  

  static
  {
    if (Main.class.getClassLoader()==ClassLoader.getSystemClassLoader())
    { 
      print
        ("WARNING: spiralcraft-core module should not in the system classpath "
        +" as this may interfere with some dynamic loading functionality"
        );
    }
    // System.setSecurityManager(new SystemSecurityManager());
  }

  /**
   * Standard entry point
   * 
   * @param args
   * @throws Throwable
   */
  public static void exec(final String[] args)
    throws Throwable
  {

    if (Spiralcraft.DEBUG)
    { 
      print
        ("spiralcraft.loader.Main.main("
        +ArrayUtil.format(args,",","\"")+")"
        );
    }
     
    final File codebase=findCodebaseContext();
    if (Spiralcraft.DEBUG)
    { 
      print("Using codebase "+codebase.toString());
      print(" ");
    }
      
    final ApplicationManager applicationManager
      =new ApplicationManager("root",codebase);
    applicationManager.setViewTitle(ArrayUtil.format(args," "," ")+" : "+codebase.toURI());

    if (Spiralcraft.DEBUG)
    { 
      
      applicationManager.setDebug(true);
      print(" ");
      print("Environment:");
      print("");
      print("  GraphicsEnvironment.isHeadless() = "
          +java.awt.GraphicsEnvironment.isHeadless()
          );
      Console console=System.console();
      print("  System.console() = "+console);
      print(" ");
      printProperty("os.name");
      printProperty("os.arch");
      printProperty("os.version");
      print(" ");
      printProperty("user.name");
      printProperty("user.home");
      printProperty("user.dir");
      print(" ");
      printProperty("java.io.tmpdir");
      printProperty("java.class.path");
      printProperty("java.home");
      print(" ");
      printProperty("spiralcraft.home");
      print(" ");
      print("  Runtime.freeMemory = "
        +Runtime.getRuntime().freeMemory()
        );
      print("  Runtime.totalMemory = "
        +Runtime.getRuntime().totalMemory()
        );
      print("  Runtime.maxMemory = "
        +Runtime.getRuntime().maxMemory()
        );
      print("  Runtime.availableProcessors = "
        +Runtime.getRuntime().availableProcessors()
        );

    
    }
    
    try
    {
      LauncherThreadGroup group
        =new LauncherThreadGroup();
        
      group.run
        (new Runnable()
        {
          @Override
          public void run()
          { 
            // Execute a single command, then exit
            ContextResourceMap contextResourceMap
              =new ContextResourceMap();
            contextResourceMap.put("codebase",codebase.toURI());
            contextResourceMap.push();
            
            try
            { 
              AbstractXmlObject<ExecutionContextProvider,?> exContext=null;
              if (Spiralcraft.EXECUTION_CONTEXT_URI!=null)
              { 
                exContext
                  =AbstractXmlObject.<ExecutionContextProvider>activate
                    (Spiralcraft.EXECUTION_CONTEXT_URI
                    ,null
                    ,new BeanFocus<ApplicationManager>(applicationManager)
                    );
                exContext.get().push();
                
              }
            
              
              try
              { applicationManager.exec(args);
              }
              finally
              {
                if (Spiralcraft.EXECUTION_CONTEXT_URI!=null)
                { 
                  exContext.get().pop();
                  try
                  { exContext.stop();
                  }
                  catch (Exception x)
                  { x.printStackTrace(ExecutionContext.getInstance().err());
                  }
                }

                
              }
              
            }
            catch (ContextualException x)
            { x.printStackTrace(ExecutionContext.getInstance().err());
            }
            catch (LaunchException x)
            { x.printStackTrace(ExecutionContext.getInstance().err());
            }
            finally
            { contextResourceMap.pop();
            }

          }
        }
        );
        
    }
    finally
    { 
      // XXX There may be threads still running that were spawned by
      //   Class inits in the LibraryClassLoader instance that will
      //   throw exceptions if they try to load classes past this point
      applicationManager.shutdown();
    }
    
  }
  
  private static final void print(String message)
  { Spiralcraft.err.println(message);
  }
  
  private static final void printProperty(String name)
  {
    print("  System.getProperty(\""+name+"\") = "
      +System.getProperty(name)
      );
  }
  
  private static File findCodebaseContext()
  {
    String fixedCodebase=System.getProperty("spiralcraft.codebase");
    if (fixedCodebase!=null)
    { return new File(fixedCodebase);
    }
    
    File codebase=null;
    File searchDir=new File(System.getProperty("user.dir"));
    while (searchDir!=null && codebase==null)
    { 
      File candidate=new File(new File(searchDir,".spiralcraft"),"Codebase");
      if (candidate.exists())
      { 
        if (Spiralcraft.DEBUG)
        { Spiralcraft.err.println("Reading codebase from "+candidate);
        }
        FileInputStream in=null;
        try
        { 
          URI codebaseURI=null;
          in=new FileInputStream(candidate);
          String codebaseSpec=new String(StreamUtil.readBytes(in)).trim();
          try
          { codebaseURI=URI.create(codebaseSpec);
          }
          catch (IllegalArgumentException x)
          { 
            throw new IllegalArgumentException
              ("Reading "+candidate+": \""+codebaseSpec+"\" is not a valid URI");
          }
          
          if (!codebaseURI.isAbsolute())
          { codebaseURI=searchDir.toURI().resolve(codebaseURI);
          }
          codebase=new File(codebaseURI);
          if (!codebase.exists())
          { 
            throw new IllegalArgumentException
              ("Reading "+candidate+": Location \""+codebase+"\" does not exist");
          }
          if (!codebase.isDirectory())
          { 
            throw new IllegalArgumentException
              ("Reading "+candidate+": Location \""+codebase+"\" is not a directory");
          }
          in.close();          
        }
        catch (IOException x)
        { x.printStackTrace();
        }
      }
      searchDir=searchDir.getParentFile();
    }
    
    if (codebase==null)
    { codebase=new File(System.getProperty("spiralcraft.home"));
    }
    
    return codebase;
  }

}
