/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.buffered;

public enum DaoTaskType {
    INSERT_MESSAGE, INSERT_CONNECTOR_MESSAGE, INSERT_MESSAGE_CONTENT, BATCH_INSERT_MESSAGE_CONTENT, EXECUTE_BATCH_INSERT_MESSAGE_CONTENT, INSERT_MESSAGE_ATTACHMENT, INSERT_META_DATA, INSERT_EVENT, STORE_MESSAGE_CONTENT, UPDATE_STATUS, UPDATE_ERRORS, UPDATE_MAPS, UPDATE_RESPONSE_MAP, MARK_AS_PROCESSED, DELETE_MESSAGE, DELETE_CONNECTOR_MESSAGES, DELETE_ALL_MESSAGES, DELETE_ALL_CONTENT, CREATE_CHANNEL, REMOVE_CHANNEL, ADD_META_DATA_COLUMN, REMOVE_META_DATA_COLUMN, RESET_STATISTICS;
}
