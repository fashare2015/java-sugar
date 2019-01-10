package com.fashare.javasuger;

import com.fashare.javasuger.annotation.Getter;
import com.fashare.javasuger.annotation.Singleton;

@Getter
@Singleton
public class UserManager {
    private User user = new User();
}
