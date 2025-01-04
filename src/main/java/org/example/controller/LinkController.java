package org.example.controller;

import org.example.application.LinkService;
import org.example.domain.Link;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

public class LinkController {
    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    public Link createLink(String originalLink, int clicksLimit, long ttl, Optional<String> user) {
        return linkService.createLink(originalLink, clicksLimit, ttl, user);
    }

    public Optional<ArrayList<Link>> findByUserId(String id) {
        return linkService.findByUserId(id);
    }

    public Optional<Set<String>> findAllUsers() {
        return linkService.findAllUsers();
    }

    public void delete(Link link) {
        linkService.delete(link);
    }

    public void update(Link link) {
        linkService.update(link);
    }

    public Optional<Link> findByHash(String shortLink) {
        return linkService.findByHash(shortLink);
    }
}
