package com.msa.authentication.inbound.adapter;

import com.msa.authentication.dto.request.JoinRequest;
import com.msa.authentication.dto.response.JoinResponse;
import com.msa.authentication.entity.User;
import com.msa.authentication.inbound.port.JoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JoinController {

    private final JoinService joinService;

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody JoinRequest joinRequest) {
        JoinResponse response = joinService.join(joinRequest);

        return ResponseEntity.ok().body(response);
    }
}
