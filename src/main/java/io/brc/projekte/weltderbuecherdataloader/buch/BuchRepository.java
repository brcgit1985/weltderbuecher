package io.brc.projekte.weltderbuecherdataloader.buch;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuchRepository extends CassandraRepository<Buch,String> { }
