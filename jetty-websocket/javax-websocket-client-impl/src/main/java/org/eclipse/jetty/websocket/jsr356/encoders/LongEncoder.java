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

package org.eclipse.jetty.websocket.jsr356.encoders;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;

/**
 * Default encoder for {@link Long} to {@link javax.websocket.Encoder.Text} Message encoder
 */
public class LongEncoder extends AbstractEncoder implements Encoder.Text<Long>
{
    @Override
    public String encode(Long object) throws EncodeException
    {
        if (object == null)
        {
            return null;
        }
        return object.toString();
    }
}
