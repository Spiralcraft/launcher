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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Loads classes contained in a specific directory tree
 */
public class FileClassResource
  extends ClassResource
{
  private final File _file;

  public FileClassResource(String file)
  { _file=new File(file);
  }

  @Override
  InputStream getResourceAsStream(String path)
  {

    try
    { return new FileInputStream(new File(_file,path));
    }
    catch (IOException x)
    { return null;
    }
  }
  
  @Override
  public URL getResource(String path)
  { 
    try
    { return _file.toURI().resolve(path).toURL();
    }
    catch (MalformedURLException x)
    { x.printStackTrace();
    }
    return null;
  }

  @Override
  byte[] loadData(String path)
    throws IOException
  {
    FileInputStream in = null;
    try
    {
      File file=new File(_file,path);
      if (!file.exists())
      { return null;
      }
      in = new FileInputStream(file);
      byte[] data = new byte[in.available()];
      in.read(data);
      return data;
    }
    finally
    { 
      if (in!=null)
      {
        try
        { in.close();
        }
        catch (IOException y)
        { }
      }
    }
  }
  
  @Override
  public void shutdown()
  {
  }
}
