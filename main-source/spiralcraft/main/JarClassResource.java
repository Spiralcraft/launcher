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
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/**
 * Loads classes contained in a specific jar
 */
public class JarClassResource
  extends ClassResource
{
  private final File _file;
  private JarFile _jarFile;

  public JarClassResource(String file)
  { _file=new File(file);
  }

  @Override
  InputStream getResourceAsStream(String path)
  { 
    try
    {
      if (_jarFile==null)
      { _jarFile=new JarFile(_file);
      }
  
      JarEntry jarEntry=_jarFile.getJarEntry(path);
      if (jarEntry==null)
      { return null;
      }
      else
      { return _jarFile.getInputStream(jarEntry);
      }
    }
    catch (IOException x)
    { return null;
    }
  }


  @Override
  public URL getResource(String path)
  {
    try
    { 
      return URI.create("jar:"+_file.toURI().toString()+"!/"+path)
        .toURL();
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
    if (_jarFile==null)
    { _jarFile=new JarFile(_file);
    }

    BufferedInputStream in = null;
    try
    {
      JarEntry jarEntry=_jarFile.getJarEntry(path);
      if (jarEntry==null)
      { return null;
      }
      
      in = new BufferedInputStream(_jarFile.getInputStream(jarEntry));

      byte[] data = new byte[(int) jarEntry.getSize()];
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
    try
    { 
      if (_jarFile!=null)
      { _jarFile.close();
      }
    }
    catch (IOException x)
    { }
  }
}
