package org.flyve.mdm.agent.core.walkthrough;

/*
 *   Copyright (C) 2017 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android-agent
 *
 * flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Rafael Hernandez
 * @date      17/8/17
 * @copyright Copyright (C) 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class WalkthroughModel {

    private String message;
    private String link;
    private int image;

    /**
     * This constructor sets the properties to equal the given arguments
     * @param string the message
     * @param string the link
     * @param int the image
     */
    public WalkthroughModel(String message, String link, int image) {
        this.message = message;
        this.link = link;
        this.image = image;
    }

    /**
     * Get the message of the Walkthrough
     * @return string the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the message of the Walkthrough
     * @param string the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the link of the Walkthrough
     * @return string the link
     */
    public String getLink() {
        return link;
    }

    /**
     * Set the link of the Walkthrough
     * @param string the link
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * Get the image of the Walkthrough
     * @return int the image
     */
    public int getImage() {
        return image;
    }

    /**
     * Set the image of the Walkthrough
     * @param int the image
     */
    public void setImage(int image) {
        this.image = image;
    }
}
