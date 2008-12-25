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
package spiralcraft.main;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads classes contained in a specific jar
 */
public abstract class ClassResource
{
  
  protected boolean debug;

  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  abstract InputStream getResourceAsStream(String path);
  
  /**
   * Load data from the repository path specified
   */
  abstract byte[] loadData(String path)
    throws IOException;
    
  /**
   * Close all resources
   */
  public abstract void shutdown();
}
