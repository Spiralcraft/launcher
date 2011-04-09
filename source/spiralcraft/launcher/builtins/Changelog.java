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

import java.io.IOException;
import java.io.PrintStream;

import spiralcraft.launcher.BuiltInReportEnvironment;
import spiralcraft.launcher.Module;
import spiralcraft.launcher.LibraryCatalog;
import spiralcraft.launcher.Resource;

/**
 * Extract the Changelog from a codebase module
 * 
 * @author mike
 *
 */
public class Changelog
  extends BuiltInReportEnvironment
{

  @Override
  protected void report(PrintStream out,String[] args)
  { 
    LibraryCatalog catalog=_applicationManager.getLibraryCatalog();
    if (args.length<1)
    { 
      out.println("A module name argument is required:");
      for (Module module: catalog.listModules())
      { out.println(module.getName());
      }
      return;
    }
    Module module=catalog.findModule(args[0]);
    if (module==null)
    { 
      out.println("Could not find module "+args[0]+":");
      for (Module candidate: catalog.listModules())
      { out.println("  "+candidate.getName());
      }
      return;
    }
    else
    { 
      Resource changeLog
        =module.getResource("META-INF/spiralcraft-scm/CHANGES.xml");
      if (changeLog!=null)
      { 
        try
        { 
          byte[] data=changeLog.getData();
          if (data!=null && data.length>0)
          { 
            out.print(new String(data));
          }
          else
          { out.print("Module "+args[0]+" changeLog does not exist");
          }
        }
        catch (IOException x)
        { out.print("Error reading changelog for module "+args[0]+": "+x.toString());
        }
      }
      else
      { 
        out.print("Module "+args[0]+" does not contain resource "+changeLog);
        
      }
    }
  }
  

  
}
