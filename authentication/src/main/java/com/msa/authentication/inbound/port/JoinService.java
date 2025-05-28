package com.msa.authentication.inbound.port;

import com.msa.authentication.dto.request.JoinRequest;
import com.msa.authentication.dto.response.JoinResponse;
import com.msa.authentication.entity.User;

public interface JoinService {

    JoinResponse join(JoinRequest request);
}
