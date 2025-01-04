package org.example.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.ILinkRepository;
import org.example.domain.Link;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

public class LinkRepository implements ILinkRepository {
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private final String KEY_DELIMETER = "#";
    private final String KEY_PREFIX = "link#";

    public LinkRepository(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.objectMapper = new ObjectMapper();
    }

    private String getKey(String userId, String shortLinkHash) {
        return KEY_PREFIX + userId + KEY_DELIMETER + shortLinkHash;
    }

    @Override
    public Link save(Link link) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = getKey(link.getUserId(), link.getShortLink());
            jedis.set(key, objectMapper.writeValueAsString(link));
        } catch (Exception e) {
            throw new RuntimeException("Error saving link to db", e);
        }
        return link;
    }

    @Override
    public Optional<Link> findByHash(String shortLinkHash) {
        try (Jedis jedis = jedisPool.getResource()) {
            String pattern = KEY_PREFIX + "*";
            Set<String> keys = jedis.keys(pattern);

            for (String key : keys) {
                String[] parts = key.split(KEY_DELIMETER);
                if (parts[2].equals(shortLinkHash)) {
                    String json = jedis.get(key);
                    if (json != null) {
                        return Optional.of(objectMapper.readValue(json, Link.class));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding link by short link hash", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<ArrayList<Link>> findByUserId(String id) {
        try (Jedis jedis = jedisPool.getResource()) {
            String pattern = KEY_PREFIX + id + KEY_DELIMETER + "*";
            Set<String> keys = jedis.keys(pattern);

            ArrayList<Link> links = new ArrayList<>();
            for (String key : keys) {
                String json = jedis.get(key);
                if (json != null) {
                    links.add(objectMapper.readValue(json, Link.class));
                }
            }
            return Optional.of(links);
        } catch (RuntimeException | JsonProcessingException e) {
            throw new RuntimeException("Error getting links for user from db", e);
        }
    }

    @Override
    public void delete(Link link) {
        String key = getKey(link.getUserId(), link.getShortLink());
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error deleting link from db", e);
        }
    }

    @Override
    public Optional<Set<String>> findAllUsers() {
        try (Jedis jedis = jedisPool.getResource()) {
            String pattern = KEY_PREFIX + "*";
            Set<String> keys = jedis.keys(pattern);

            Set<String> userIds = new HashSet<>();
            for (String key : keys) {
                String[] parts = key.split(KEY_DELIMETER);
                userIds.add(parts[1]);
            }
            return Optional.of(userIds);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error getting user ids from db", e);
        }
    }
}
