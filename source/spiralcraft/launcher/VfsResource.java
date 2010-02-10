//
// Copyright (c) 1998,2010 Michael Toth
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

import spiralcraft.log.ClassLog;
import spiralcraft.vfs.AbstractResource;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.UnresolvableURIException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class VfsResource
  extends AbstractResource
  implements Container
{
  private static final ClassLog log
    =ClassLog.getInstance(VfsResource.class);
  
  private final LibraryCatalog catalog;
  private Resource resource;
  private final String _path;
  private List<String> _contents;

  private static final URI stripTrailingSlash(URI uri)
  { 
    if (uri.getPath()==null)
    { 
      throw new IllegalArgumentException
        ("URI "+uri+" has a null path component");
    }
    
    if (uri.getPath().endsWith("/"))
    { 
      try
      {
        return new URI
          (uri.getScheme()
          ,null
          ,uri.getPath().substring(0,uri.getPath().length()-1)
          ,null
          ,null
          );
      }
      catch (URISyntaxException x)
      { throw new IllegalArgumentException("Failed to parse URI "+uri);
      }
    }
    else
    { return uri;
    }
  }
  
  public VfsResource(URI uri,LibraryCatalog catalog)
  { 
    super(stripTrailingSlash(uri));
    _path=getURI().getPath().substring(1);
    this.catalog=catalog;
  }

  @Override
  public InputStream getInputStream()
    throws IOException
  { 
    resource
      =catalog.findResource(_path);
    if (resource!=null)
    { return new ByteArrayInputStream(resource.getData());
    }
    else
    { throw new IOException("Resource '"+_path+"' not found");
    }
  }

  @Override
  public boolean supportsRead()
  { return true;
  }

  @Override
  public OutputStream getOutputStream()
    throws IOException
  { throw new UnsupportedOperationException("Module library is read-only");
  }

  @Override
  public boolean supportsWrite()
  { return false;
  }
  
  @Override
  public Container asContainer()
  { 
    boolean debug=false;
    try
    {
      if (_contents==null)
      { 
        if (debug)
        { log.fine("Checking "+_path+" in "+catalog);
        }
        Iterator<Resource> resources=catalog.findResources(_path);
        
        List<URI> parts=new LinkedList<URI>();
        List<String> contents=new LinkedList<String>();
        while (resources.hasNext())
        { 
          Resource resource=resources.next();
          try
          { 
            URI uri=resource.getResource().toURI();
            parts.add(uri);
          }
          catch (URISyntaxException x)
          { x.printStackTrace();
          }
          
          if (debug)
          { log.fine("Got "+parts.get(parts.size()-1).toString());
          }
        }
        
        
        if (parts.size()>0)
        { 
          for (URI uri: parts)
          { 
            VfsResource partResource
              =(VfsResource) Resolver.getInstance().resolve(uri);
            if (debug)
            { log.fine("Checking part "+partResource);
            }
              
            Container partContainer=partResource.asContainer();
            if (partContainer!=null)
            {
              if (debug)
              { log.fine("Listing container part "+partContainer);
              }
                
              for (spiralcraft.vfs.Resource resource: partContainer.listChildren())
              { 
                 
                if (debug)
                { log.fine("Adding child  "+resource);
                }
                contents.add(resource.getURI().getPath());
              }
            }
          }
          if (contents.size()>0)
          { this._contents=contents;
          }
        }
      }
    }
    catch (IOException x)
    { x.printStackTrace();
    }
    
    return _contents!=null?this:null;
  }

  
  public void renameTo(URI name)
  { 
    throw new UnsupportedOperationException
      ("A classpath resource cannot be renamed");
  }  

  @Override
  public boolean exists()
    throws IOException
  { return catalog.findResource(_path)!=null;
  }
  
  public void delete()
    throws IOException
  { throw new IOException("ClasspathResource is read-only");
  }

  
  @Override
  public VfsResource getChild(String name)
  { 
    return new VfsResource
      (URI.create(getURI().getScheme()+":/"+_path+"/"+name)
      ,catalog
      );
  }
  
  @Override
  public VfsResource asResource()
  { return this;
  }

  @Override
  public VfsResource createLink(
    String name,
    spiralcraft.vfs.Resource resource)
    throws UnresolvableURIException
  { return null;
  }

  @Override
  public VfsResource[] listChildren()
    throws IOException
  { 
    VfsResource[] children=new VfsResource[_contents.size()];
    int i=0;
    for (String childName:_contents)
    { children[i++]=getChild(childName);
    }
    return children;
  }

  @Override
  public VfsResource[] listContents()
    throws IOException
  { return listChildren();
  }

  @Override
  public VfsResource[] listLinks()
    throws IOException
  { return null;
  }
}
