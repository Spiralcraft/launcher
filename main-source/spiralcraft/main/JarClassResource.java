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
  private boolean closed;

  public JarClassResource(String file)
    throws IOException
  { 
    _file=new File(file);
    if (!_file.exists())
    { throw new IOException("File "+file+" does not exist");
    }
  }

  @Override
  public String getClassPath()
  { return _file.getPath();
  }
  
  private void assertOpen()
  { 
    if (closed)
    { throw new IllegalStateException("JarClassResource: "+_file+" is closed");
    }
  }
  
  @Override
  InputStream getResourceAsStream(String path)
  { 
    assertOpen();
    try
    {
      openJar();
  
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
    finally
    {
      if (closed)
      { shutdown();
      }
    }
  }


  @Override
  public URL getResource(String path)
  {
    assertOpen();
    try
    { 
      openJar();
      JarEntry jarEntry=_jarFile.getJarEntry(path);
      
      if (jarEntry==null)
      { return null;
      }
      return URI.create("jar:"+_file.toURI().toString()+"!/"+path)
        .toURL();
    }
    catch (IOException x)
    { return null;
    }
  }
  
  @Override
  byte[] loadData(String path)
    throws IOException
  {
    assertOpen();
    openJar();

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
      if (closed)
      { shutdown();
      }
    }
  }
  
  private void openJar()
    throws IOException
  {
    if (_jarFile==null)
    { 
      _jarFile=new JarFile(_file,false,JarFile.OPEN_READ);
      // System.err.println("Opened jar "+_jarFile.getName());
    }
    
  }
  
  @Override
  public void shutdown()
  { 
    try
    { 

      closed=true;
      if (_jarFile!=null)
      { 
        // System.err.println("Closing jar "+_jarFile.getName());
        _jarFile.close();
      }
      _jarFile=null;
    }
    catch (IOException x)
    { }
  }
}
