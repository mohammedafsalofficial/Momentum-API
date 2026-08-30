package com.momentum.api.auth.facade;

import com.momentum.api.auth.model.User;
import org.springframework.security.core.Authentication;

public interface IAuthenticationFacade {

    Authentication getAuthentication();

    User getAuthenticatedUser();
}
