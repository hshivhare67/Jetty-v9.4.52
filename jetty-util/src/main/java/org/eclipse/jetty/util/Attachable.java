//
//  ========================================================================
//  Copyright (c) 1995-2022 Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.util;

/**
 * Abstract mechanism to support attachment of miscellaneous objects.
 */
public interface Attachable
{
    /**
     * @return the object attached to this instance
     * @see #setAttachment(Object)
     */
    Object getAttachment();

    /**
     * Attaches the given object to this stream for later retrieval.
     *
     * @param attachment the object to attach to this instance
     */
    void setAttachment(Object attachment);
}
