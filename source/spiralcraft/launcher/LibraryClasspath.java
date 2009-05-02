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
package spiralcraft.launcher;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Resolves class and resource data from a set of libraries
 *   in a LibraryCatalog
 */
public interface LibraryClasspath
{

  byte[] loadData(String path)
    throws IOException;
  
  URL getResource(String path)
    throws IOException;

  Enumeration<URL> getResources(String path)
    throws IOException;
  
  void addLibrary(String path)
    throws IOException;

  /**
   * Return the path of the native library in the catalog with the
   *   specified name
   */
  String findNativeLibrary(String name);
  
  /**
   * Resolve the most recent library which contains the resource, and
   *   all the other libraries on which that library depends.
   */
  void resolveLibrariesForResource(String name)
    throws IOException;

  /**
   * Add the libraries associated with the specified module
   *   to the class path
   */
  void addModule(String moduleName)
    throws IOException;
    
  /**
   * Release any resources we have allocated, such as open libraries
   */
  void release();
  
  void setDebug(boolean debug);
  
  /**
   * Add the latest versions of all the modules in the library to the
   *   classpath
   */
  void addAllModules()
    throws IOException;
  
}
