package com.website.backend.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

/**
 * 用户实体类，对应系统用户表
 */
@Entity
@Data
public class User {

	/** 主键ID */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 用户名，唯一 */
	@Column(nullable = false, unique = true)
	private String username;

	/** 密码（加密存储） */
	@Column(nullable = false)
	private String password;

	/** 用户角色集合，EAGER 立即加载 */
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	/**
	 * 添加角色到用户
	 * @param role 角色对象
	 */
	public void addRole(Role role) {
		this.roles.add(role);
	}

}