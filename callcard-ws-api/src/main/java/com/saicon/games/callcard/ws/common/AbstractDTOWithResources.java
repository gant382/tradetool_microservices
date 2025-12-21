package com.saicon.games.common;

import com.saicon.games.callcard.util.DTOParam;
import com.saicon.resources.dto.ResourceDTO;

/**
 * A class to be extended by DTO classes that want to carry resources with them.
 *
 * @author Antonis Thodis
 */
public abstract class AbstractDTOWithResources extends AbstractDTOWithMetadata {

    /**
     * Array of <code>ResourceDTO</code> objects, representing the requested resources.
     */
    @DTOParam(100)
    private ResourceDTO[] resources;

    public ResourceDTO[] getResources() {
        return resources;
    }

    public void setResources(ResourceDTO[] resources) {
        this.resources = resources;
    }

    abstract public String getItemIdForResourceLookup();

    abstract public int getItemTypeIdForResourceLookup();

}
