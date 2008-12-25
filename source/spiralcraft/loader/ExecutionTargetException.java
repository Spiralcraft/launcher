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

import spiralcraft.exec.ExecutionException;

/**
 * Thrown when something goes wrong during execution
 */
public class ExecutionTargetException
  extends ExecutionException
{
  private static final long serialVersionUID=1;
  
  private final Throwable _targetException;
  
  public ExecutionTargetException(Throwable targetException)
  { 
    super("");
    _targetException=targetException;
    if (_targetException!=null)
    { initCause(_targetException);
    }
  }
  
  public Throwable getTargetException()
  { return _targetException;
  }
  
  @Override
  public String toString()
  { return super.toString()+" caused by "+_targetException.toString();
  }
}
