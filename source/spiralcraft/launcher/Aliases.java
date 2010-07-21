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

import java.util.HashMap;

import spiralcraft.util.ArrayUtil;
import spiralcraft.util.string.StringUtil;


/**
 * A map of individual Strings to command-line format expansions, with support
 *   for hierarchical defaults. 
 */
public class Aliases
{
  private final Aliases _parent;
  private final HashMap<String,String> _map
    =new HashMap<String,String>();

  public Aliases(Aliases parent)
  { _parent=parent;
  }

  public Aliases()
  { _parent=null;
  }



  /**
   * Match the first element of the source array with an expansion
   *   and, if found, replace the first element with the expansion
   *   and return the result. If not found, return the source array
   */
  public synchronized String[] expand(String[] source)
  {
    if (source.length<1)
    { return source;
    }

    String expansion;
    synchronized (_map)
    { expansion=_map.get(source[0]);
    }

    if (expansion==null)
    { 
      if (_parent!=null)
      { return _parent.expand(source);
      }
      else
      { return source;
      }
    }
    else
    {
      return ArrayUtil.concat
        (StringUtil.tokenizeCommandLine(expansion)
        ,ArrayUtil.truncateBefore(source,1)
        );
    }
    
  }
}
