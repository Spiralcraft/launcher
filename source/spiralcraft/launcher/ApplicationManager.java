//
// Copyright (c) 1998,2008 Michael Toth
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

import spiralcraft.registry.Registry;
import spiralcraft.registry.RegistryNode;

import spiralcraft.lang.BindException;
//import spiralcraft.log.ClassLogger;

import spiralcraft.util.ArrayUtil;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;

import spiralcraft.data.persist.AbstractXmlObject;

import java.io.File;
import java.io.IOException;

import java.net.URI;
//import java.util.logging.Logger;

/**
 * <p>Controls the execution of applications via one or more
 *   ApplicationEnvironments.
 * </p>
 * 
 * <p>An ApplicationEnvironment defines the code modules (.jar or directory trees)
 *   and an entry point necessary to run an application.
 * </p>
 *
 * <p>
 * ApplicationEnvironments are stored in Resources identified by a URI, which
 *   is provided to the exec() method.
 * </p>
 *
 * <p>
 * If the URI is relative, the ApplicationManager will search for the
 *   Resource according to the following rules:
 *
 *   <ol>
 *     <li>"user.dir" Java system property
 *     </li>
 *     <li>User environment path (<user-home>/.spiralcraft/env)
 *     </li>
 *     <li>System environment path (<spiralcraft-home>/env)
 *     </li>
 *   </ol>
 * </p>
 * 
 * @author mike
 */
public class ApplicationManager
{

//  private static final Logger log
//    =ClassLogger.getInstance(ApplicationManager.class);
  
  private static RegistryNode _REGISTRY_ROOT
    =Registry.getLocalRoot().createChild("applicationManager");

    
  private final File _codebase;
      
  private final LibraryCatalog _catalog;
  
  private final String _userId;
  private final RegistryNode _registryNode;

  private final URI _codebaseEnvironmentURI;


  private final URI _userHomeEnvironmentURI
    =new File(System.getProperty("user.home"))
      .toURI().resolve(".spiralcraft/env/");

  private int _nextEnvironmentId=0;

  private boolean debug=false;
  

  public ApplicationManager(String userId,File codebase)
  { 
    _userId=userId;
    _codebase=codebase;
    _codebaseEnvironmentURI=_codebase.toURI().resolve("env/");
    _catalog=
      new LibraryCatalog
        (new File(_codebase,"lib")
        );
    _registryNode=_REGISTRY_ROOT.createChild(_userId);
  }
  
  public void setDebug(boolean val)
  { debug=val;
  }
  
  public void shutdown()
  { _catalog.close();
  }
  
  public LibraryCatalog getLibraryCatalog()
  { return _catalog;
  }

  public void exec(String[] args)
    throws ExecutionTargetException
  { 
    if (args.length==0)
    { 
      // Show environments in-scope
      throw new IllegalArgumentException
        ("Please specify an application environment");
    }
    else if (args[0].equals("exec"))
    {
      // Default environment
      ApplicationEnvironment environment=new ApplicationEnvironment();
      environment.resolve(this);
      args[0]=args[1]; // Instance 
      
      args=(String[]) ArrayUtil.truncateBefore(args,1);
      environment.exec(args);
    }
    else
    {

      URI applicationURI=findEnvironment(args[0],".env.xml");
      if (applicationURI==null)
      { 
        // Show environments in-scope
        throw new IllegalArgumentException
          ("Unknown application environment '"+args[0]+"'");
      }

      args=(String[]) ArrayUtil.truncateBefore(args,1);

      try
      {
        AbstractXmlObject<ApplicationEnvironment,?> environmentRef
        =AbstractXmlObject.<ApplicationEnvironment>create
        (null
          ,applicationURI
          ,_registryNode.createChild(Integer.toString(_nextEnvironmentId++))
          ,null
        );


        ApplicationEnvironment environment=environmentRef.get();
        environment.resolve(this);

        environment.exec(args);
      }
      catch (BindException x)
      { throw new ExecutionTargetException(x);
      }
    }
  }

  /**
   * Search for the named environment, in order of priority:
   *   
   *   1. The user directory
   *   2. The user environment path
   *   3. The system environment path
   *
   */
  private URI findEnvironment(String name,String suffix)
  {
    URI nameURI=URI.create(name+suffix);
    URI searchURI=null;
    
    
    if (nameURI.isAbsolute() && isEnvironment(nameURI))
    { return nameURI;
    }

    
    searchURI=new File(System.getProperty("user.dir")).toURI().resolve(nameURI);
    if (isEnvironment(searchURI))
    { return searchURI;
    }

    searchURI=_userHomeEnvironmentURI.resolve(nameURI);
    if (isEnvironment(searchURI))
    { return searchURI;
    }

    searchURI=_codebaseEnvironmentURI.resolve(nameURI);
    if (isEnvironment(searchURI))
    { return searchURI;
    }



    return null;
  }

  private boolean isEnvironment(URI uri)
  {
    if (debug)
    { System.err.println("Searching for "+uri);
    }
    
    try
    { 
      Resource resource = Resolver.getInstance().resolve(uri);
      if (resource.exists())
      { 
        if (debug)
        { System.err.println("Found "+uri);
        }
        return true;
      }
      else
      { return false;
      }
    }
    catch (UnresolvableURIException x)
    { System.err.println(x.toString());
    }
    catch (IOException x)
    { System.err.println(x.toString());
    }
    return false;
  }
}


