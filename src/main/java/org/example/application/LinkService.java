package org.example.application;

import org.example.config.SystemConfig;
import org.example.domain.Link;
import org.example.infra.LinkRepository;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class LinkService {
    private final LinkRepository linkRepository;
    private final SystemConfig config;

    public LinkService(LinkRepository linkRepository, SystemConfig config) {
        this.linkRepository = linkRepository;
        this.config = config;
    }

    public Link createLink(String originalLink, int clicksLimit, long ttl, Optional<String> user) {
        String userId = user.orElseGet(() -> UUID.randomUUID().toString());
        String shortLink = Integer.toString((userId + originalLink).hashCode(), 36);

        Link link = Link.builder()
                .userId(userId)
                .originalLink(originalLink)
                .shortLink(shortLink)
                .ttl(Math.min(config.getLinkTTL(), ttl)) // выбираем меньшее между значениями из конфига и введенного юзером
                .clicksLimit(Math.max(config.getClicksMinimum(), clicksLimit)) // выбираем большее между значениями из конфига и введенного юзером
                .build();

        return linkRepository.save(link);
    }

    public Optional<ArrayList<Link>> findByUserId(String id) {
        return linkRepository.findByUserId(id);
    }

    public Optional<Set<String>> findAllUsers() {
        return linkRepository.findAllUsers();
    }

    public void delete(Link link) {
        linkRepository.delete(link);
    }

    public void update(Link link) {
        linkRepository.save(link);;
    }

    public Optional<Link> findByHash(String shortLink) {
        String hash = shortLink.replaceFirst(config.getLinkBaseUrl(), "");
        return linkRepository.findByHash(hash);
    }
}
