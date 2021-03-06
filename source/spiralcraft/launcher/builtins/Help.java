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
import java.io.InputStream;
import java.io.PrintStream;

import spiralcraft.launcher.BuiltInReportEnvironment;
import spiralcraft.vfs.StreamUtil;

/**
 * Help info for launcher
 * 
 * @author mike
 *
 */
public class Help
  extends BuiltInReportEnvironment
{

  @Override
  protected void report(PrintStream out,String[] args)
  { 
    InputStream in=Help.class.getResourceAsStream("usage.txt");
    try
    {
      try
      {
        StreamUtil.copyRaw(in,out,8192,-1);
      }
      finally
      { in.close();
      }
    }
    catch (IOException x)
    { x.printStackTrace();
    }
    out.println(" ");
  }
  
}
