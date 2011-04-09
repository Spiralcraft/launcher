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

import spiralcraft.launcher.BuiltInReportEnvironment;
import spiralcraft.launcher.Module;
import spiralcraft.launcher.LibraryCatalog;
import spiralcraft.launcher.Resource;

/**
 * Info about module versions in codebase
 * 
 * @author mike
 *
 */
public class Versions
  extends BuiltInReportEnvironment
{

  @Override
  protected void report(PrintStream out,String[] args)
  {
    LibraryCatalog catalog=_applicationManager.getLibraryCatalog();
    
    List<Module> modules=catalog.listModules();
    for (Module module : modules)
    { 
      out.println
        ("Module: "+module.getName());
      
      out.println(" ");
      
      Resource versionResource
        =module.getResource("META-INF/spiralcraft-scm/version.properties");
      if (versionResource!=null)
      { 
        Properties properties=new Properties();
        try
        { 
          properties.load(new ByteArrayInputStream(versionResource.getData()));
          out.println("    version: "+properties.getProperty("version"));
          out.println("      stamp: "+properties.getProperty("build.name"));
        }
        catch (IOException x)
        { 
        }
      }
      out.println("       path: "
        +module.getPath());
      out.println("       time: "
        +new Date(module.getLastModified()));
      out.println(" ");
      
    }
    
    
  }
  
}
