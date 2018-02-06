package projector.model;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

public abstract class BaseEntity extends AbstractModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @DatabaseField(generatedId = true, index = true)
    private Long id;

    public BaseEntity() {
        this(null);
    }

    public BaseEntity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
