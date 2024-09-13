package com.light.mapper.entity;

import java.io.Serializable;

public interface IEntity extends Serializable {
    /**
     * @return Metadata
     * 
     */
    public Metadata metadata();

    /**
     * @return primary key value
     *
     */
    public Object pkValue();
}
