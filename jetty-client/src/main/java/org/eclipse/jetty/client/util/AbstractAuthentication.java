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

package org.eclipse.jetty.client.util;

import java.net.URI;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;

public abstract class AbstractAuthentication implements Authentication
{
    private final URI uri;
    private final String realm;

    public AbstractAuthentication(URI uri, String realm)
    {
        this.uri = uri;
        this.realm = realm;
    }

    public abstract String getType();

    public URI getURI()
    {
        return uri;
    }

    public String getRealm()
    {
        return realm;
    }

    @Override
    public boolean matches(String type, URI uri, String realm)
    {
        if (!getType().equalsIgnoreCase(type))
            return false;

        if (!this.realm.equals(ANY_REALM) && !this.realm.equals(realm))
            return false;

        return matchesURI(this.uri, uri);
    }

    public static boolean matchesURI(URI uri1, URI uri2)
    {
        String scheme = uri1.getScheme();
        if (scheme.equalsIgnoreCase(uri2.getScheme()))
        {
            if (uri1.getHost().equalsIgnoreCase(uri2.getHost()))
            {
                // Handle default HTTP ports.
                int thisPort = HttpClient.normalizePort(scheme, uri1.getPort());
                int thatPort = HttpClient.normalizePort(scheme, uri2.getPort());
                if (thisPort == thatPort)
                {
                    // Use decoded URI paths.
                    return uri2.getPath().startsWith(uri1.getPath());
                }
            }
        }
        return false;
    }
}
