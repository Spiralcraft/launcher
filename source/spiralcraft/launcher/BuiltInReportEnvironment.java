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
package spiralcraft.launcher;

import java.io.IOException;
import java.io.PrintStream;

import spiralcraft.launcher.ApplicationEnvironment;
import spiralcraft.launcher.LaunchException;

/**
 * <p>Runs a routine that outputs information to the standard output
 * </p>
 * 
 * @author mike
 *
 */
public abstract class BuiltInReportEnvironment
  extends ApplicationEnvironment
{

  @Override
  public void exec(String[] args)
    throws LaunchException
  { 
    try
    {
      pushStreams();
      report(outStream,args);
      outStream.flush();
      popStreams();
    }
    catch (IOException e)
    { throw new LaunchException(e);
    }
  }
  
  protected abstract void report(PrintStream out,String[] args);
  
}
