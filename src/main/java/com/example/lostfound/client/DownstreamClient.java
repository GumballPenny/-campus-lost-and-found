package com.example.lostfound.client;

import java.net.URI;
import java.time.Duration;

public record DownstreamClient(URI baseUrl, Duration timeout) {
    // Lab 4 binds configuration only. The real HTTP call is added in Lab 6.
}
