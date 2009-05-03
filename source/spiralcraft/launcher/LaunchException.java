//
// Copyright (c) 1998,2009 Michael Toth
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

public class LaunchException
  extends Exception
{

  private static final long serialVersionUID = 2748927464501796324L;

  public LaunchException(String message)
  { super(message);
  }
  
  public LaunchException(String message,Throwable targetException)
  { super(message,targetException);
  }
  
  public LaunchException(Throwable targetException)
  { super(targetException);
  }
}
