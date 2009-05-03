//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.launcher.builtins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import spiralcraft.launcher.ApplicationEnvironment;
import spiralcraft.launcher.LaunchException;
import spiralcraft.launcher.Module;
import spiralcraft.launcher.LibraryCatalog;
import spiralcraft.launcher.Resource;

/**
 * Extract the Changelog a codebase module
 * 
 * @author mike
 *
 */
public class Changelog
  extends ApplicationEnvironment
{

  @Override
  public void exec(String[] args)
    throws LaunchException
  { 
    LibraryCatalog catalog=_applicationManager.getLibraryCatalog();
    if (args.length<1)
    { throw new LaunchException("A module name argument is required");
    }
    Module module=catalog.findModule(args[0]);
    if (module==null)
    { throw new LaunchException("Could not find module "+args[0]);
    }
    else
    { 
      Resource changeLog
        =module.getResource("META-INF/spiralcraft-scm/CHANGES.xml");
      if (changeLog!=null)
      { 
        try
        { System.out.write(changeLog.getData());
        }
        catch (IOException x)
        { throw new LaunchException(x);
        }
      }
    }
  }
  
  public void dump(PrintStream out)
  {
    LibraryCatalog catalog=_applicationManager.getLibraryCatalog();
    
    List<Module> modules=catalog.listModules();
    for (Module module : modules)
    { 
      out.println
        (module.getName());
      
      out.println("       path: "
        +module.getPath());
      out.println("       time: "
        +new Date(module.getLastModified()));
      
      Resource versionResource
        =module.getResource("META-INF/spiralcraft-scm/version.properties");
      if (versionResource!=null)
      { 
        Properties properties=new Properties();
        try
        { 
          properties.load(new ByteArrayInputStream(versionResource.getData()));
          out.println("    version: "+properties.getProperty("build.name"));
        }
        catch (IOException x)
        { 
        }
      }
    }
    
    
  }
  
}
