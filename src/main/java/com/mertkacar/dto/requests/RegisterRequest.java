package com.mertkacar.dto.requests;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
   private  String username;
   private  String email;
   private  String firstName;
   private  String lastName;
   private  String password;
   private  String userCode;      // Token’a koymak istediğin özel alan
   private  List<String> roles;   // Örn: ["USER"] veya ["ADMIN","USER"]
    private List<String> clientRoles;    // permission gibi (READER, WRITER)
}

