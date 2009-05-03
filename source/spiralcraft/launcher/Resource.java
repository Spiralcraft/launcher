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
// 
package spiralcraft.launcher;

import java.io.IOException;
import java.net.URL;

public abstract class Resource
{
  Module module;
  String name;

  public abstract byte[] getData()
    throws IOException;

  public abstract URL getResource()
    throws IOException;
}
