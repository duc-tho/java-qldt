package com.nhom11.qldt.feature.auth.view;

import java.util.List;

import org.springframework.stereotype.Component;

import com.nhom11.qldt.core.table.ActionDef;
import com.nhom11.qldt.core.table.BaseTableDefinition;
import com.nhom11.qldt.core.table.ColumnDef;
import com.nhom11.qldt.feature.auth.model.entity.User;

@Component
public class UserTableDefinition extends BaseTableDefinition<User> {

    @Override
    public List<ColumnDef> columns() {
        return List.of(
                new ColumnDef("id", "users.column.id", user -> String.valueOf(((User) user).getId()), false,
                        false, true),
                new ColumnDef("fullName", "users.column.full_name", user -> ((User) user).getFullName(), true,
                        true, true),
                new ColumnDef("userCode", "users.column.user_code", user -> ((User) user).getUserCode(), true,
                        true, true),
                new ColumnDef("department", "users.column.department", user -> ((User) user).getDepartment() == null ? "" : ((User) user).getDepartment().getName(), false,
                        true, false),
                new ColumnDef("email", "users.column.email", user -> ((User) user).getEmail(), true, true, true),
                new ColumnDef("username", "users.column.username", user -> ((User) user).getUsername(), true, true,
                        true),
                new ColumnDef("phone", "users.column.phone", user -> ((User) user).getPhone(), true, true, true));
    }

    @Override
    public List<ActionDef> actions(String baseUrl) {
        return List.of(
                new ActionDef.Link("users.action.edit", "bi:pencil-square",
                        user -> baseUrl + "/" + ((User) user).getId() + "/edit"),
                new ActionDef.Delete("users.action.delete", "bi:trash",
                        user -> baseUrl + "/" + ((User) user).getId(),
                        user -> ((User) user).getFullName()));
    }
}
