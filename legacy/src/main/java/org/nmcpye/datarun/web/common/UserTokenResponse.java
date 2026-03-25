package org.nmcpye.datarun.web.common;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@Setter
@Getter
public class UserTokenResponse {

    private String id;
    private String uid;
    private String login;
    private String firstName;
    private String lastName;
    private String email;
    private String imageUrl;
    private boolean activated;
    private String langKey;
    private String createdBy;
    private String createdDate;
    private String lastModifiedBy;
    private String lastModifiedDate;
    private Set<String> authorities;
//    private String token;

    private String authType;

//    public String getToken() {
//        return token;
//    }

//    public void setToken(String token) {
//        this.token = token;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserTokenResponse that = (UserTokenResponse) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getLogin(), that.getLogin());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getId());
        result = 31 * result + Objects.hashCode(getLogin());
        return result;
    }
}
