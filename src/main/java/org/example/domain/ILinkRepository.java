package org.example.domain;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

public interface ILinkRepository {
    Link save(Link link);
    Optional<Link> findByHash(String shortLink);
    Optional<ArrayList<Link>> findByUserId(String id);
    void delete(Link link);
    Optional<Set<String>> findAllUsers();
}
