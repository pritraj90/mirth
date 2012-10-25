/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.passthru;

import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;

public class PassthruDaoFactory implements DonkeyDaoFactory {
    @Override
    public PassthruDao getDao() {
        return new PassthruDao();
    }
}
