# Bithumb API Example

This is the official Bithumb API example that shows proper JWT authentication:

```java
package com.example.sample;

// https://mvnrepository.com/artifact/com.auth0/java-jwt
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
// https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class GETNoArgs {

    public static void main(String[] args) {
        String accessKey = "발급받은 API KEY";
        String secretKey = "발급받은 SECRET KEY";
        String apiUrl = "https://api.bithumb.com";

        // Generate access token
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("timestamp", System.currentTimeMillis())
                .sign(algorithm);
        String authenticationToken = "Bearer " + jwtToken;

        // Call API
        final HttpGet httpRequest = new HttpGet(apiUrl + "/v1/status/wallet");
        httpRequest.addHeader("Authorization", authenticationToken);

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(httpRequest)) {
            // handle to response
            int httpStatus = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            System.out.println(httpStatus);
            System.out.println(responseBody);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

## Key Points:
1. Uses JWT with HMAC256 algorithm
2. Requires access_key, nonce (UUID), and timestamp claims
3. Authorization header format: "Bearer {jwt_token}"
4. Base URL: https://api.bithumb.com
5. Example endpoint: /v1/status/wallet
