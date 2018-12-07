/*******************************************************************************
 * Copyright (c) 2015,  Stefan Andres.  All rights reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *******************************************************************************/
package de.myandres.optolink.viessmann;

import de.myandres.optolink.config.Config;
import de.myandres.optolink.entity.Telegram;
import de.myandres.optolink.optointerface.OptolinkException;
import de.myandres.optolink.optointerface.OptolinkInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ViessmannHandlerURLImpl extends ViessmannHandlerImpl {
    private static Logger log = LoggerFactory.getLogger(ViessmannHandlerURLImpl.class);
    private OptolinkInterface optolinkInterface;

    ViessmannHandlerURLImpl(Config config, OptolinkInterface optolinkInterface) {
        super(config, optolinkInterface);
        this.optolinkInterface = optolinkInterface;
    }

    @Override
    public synchronized Optional<String> setValue(Telegram telegram, String value) {
        Optional<String> result = Optional.empty();
        try {
            optolinkInterface.open();
            result = super.setValue(telegram, value);
        } catch (OptolinkException e) {
            log.error("Opening TTY type URL failed");
        } finally {
            optolinkInterface.close();
        }
        return result;
    }

    @Override
    public synchronized Optional<String> getValue(Telegram telegram) {
        Optional<String> result = Optional.empty();
        try {
            optolinkInterface.open();
            result = super.getValue(telegram);
        } catch (Exception e) {
            log.error("Opening TTY type URL failed");
        } finally {
            optolinkInterface.close();
        }
        return result;

    }
}
