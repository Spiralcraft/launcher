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
package spiralcraft.loader;

import spiralcraft.main.Spiralcraft;

import spiralcraft.util.ArrayUtil;
import spiralcraft.vfs.StreamUtil;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.net.URI;


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
      Spiralcraft.err.println
        ("WARNING: spiralcraft-core module should not in the system classpath "
        +" as this may interfere with some dynamic loading functionality"
        );
    }
    // System.setSecurityManager(new SystemSecurityManager());
  }

  public static void exec(String[] args)
    throws Throwable
  {

    if (Spiralcraft.DEBUG)
    { 
      Spiralcraft.err.println
        ("spiralcraft.loader.Main.main("+ArrayUtil.format(args,",","\"")+")");
    }
     
    File codebase=findCodebaseContext();
    if (Spiralcraft.DEBUG)
    { Spiralcraft.err.println("Using codebase "+codebase.toString());
    }
      
    ApplicationManager applicationManager
      =new ApplicationManager("root",codebase);
      

    if (Spiralcraft.DEBUG)
    { applicationManager.setDebug(true);
    }
    
    try
    {
      if (args.length>0)
      { 
        // Execute a single command, then exit
        applicationManager.exec(args);
      }
      else
      { Spiralcraft.err.println(new Usage().toString());
      }
    }
    catch (ExecutionTargetException x)
    { throw x.getTargetException();
    }
    finally
    { 
      // XXX Make sure this runs on the ExecutionTargetException rethrow
      applicationManager.shutdown();
    }
    
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
