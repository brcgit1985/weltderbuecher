package io.brc.projekte.weltderbuecherdataloader.buch;

import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDate;
import java.util.List;

@Table(value = "buch_by_id")
public class Buch {
    @Id
    @PrimaryKeyColumn(name = "buch_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String id;

    @Column("buch_name")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String name;

    @Column("buch_beschreibung")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String beschreibung;

    @Column("buch_veroeffentlichungsdatum")
    @CassandraType(type = CassandraType.Name.DATE)
    private LocalDate datum;

    @Column("buch_bildids")
    @CassandraType(type = CassandraType.Name.LIST, typeArguments = CassandraType.Name.TEXT)
    private List<String> bildIds;

    @Column("buch_authornamen")
    @CassandraType(type = CassandraType.Name.LIST, typeArguments = CassandraType.Name.TEXT)
    private List<String> authorNamen;

    @Column("buch_authorids")
    @CassandraType(type = CassandraType.Name.LIST, typeArguments = CassandraType.Name.TEXT)
    private List<String> authorIds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public List<String> getBildIds() {
        return bildIds;
    }

    public void setBildIds(List<String> bildIds) {
        this.bildIds = bildIds;
    }

    public List<String> getAuthorNamen() {
        return authorNamen;
    }

    public void setAuthorNamen(List<String> authorNamen) {
        this.authorNamen = authorNamen;
    }

    public List<String> getAuthorIds() {
        return authorIds;
    }

    public void setAuthorIds(List<String> authorIds) {
        this.authorIds = authorIds;
    }
}

