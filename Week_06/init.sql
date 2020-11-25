

drop table if exists mall_cart;

drop table if exists mall_category;

drop table if exists mall_order;

drop table if exists mall_order_item;

drop table if exists mall_pay_info;

drop table if exists mall_product;

drop table if exists mall_shipping;

drop table if exists mall_user;

/*==============================================================*/
/* Table: mall_cart                                            */
/*==============================================================*/
create table mall_cart
(
   id                   int(11) not null AUTO_INCREMENT,
   user_id              int(11) not null comment '用户ID',
   product_id           int(11) DEFAULT NULL comment '产品ID',
   quantity             int(11) DEFAULT NULL comment '数量',
   checked              tinyint(1) DEFAULT NULL comment '是否勾选，1=勾选，0=不勾选',
   create_time          timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   update_time          timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
   primary key (id),
   KEY `user_id_index` (user_id) USING BTREE
)ENGINE=InnoDB AUTO_INCREMENT=121 DEFAULT CHARSET=UTF8 COMMENT '购物车表';



/*==============================================================*/
/* Table: mall_category                                        */
/*==============================================================*/
create table mall_category
(
   id                   int(11) not null AUTO_INCREMENT,
   parent_id            int(11) DEFAULT null comment '父品类id',
   name                 varchar(50) DEFAULT NULL comment '品类名称',
   status               tinyint(4) DEFAULT 1 comment '品类状态 1=正常, 2=已废弃',
   sort_order           int(4) DEFAULT NULL comment '排序号',
   create_time          timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   update_time          timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
   primary key (id)
)ENGINE=InnoDB AUTO_INCREMENT=100032 DEFAULT CHARSET=UTF8 COMMENT = '商品品类表';



/*==============================================================*/
/* Table: mall_order                                           */
/*==============================================================*/
create table mall_order
(
   id                   int(11) not null AUTO_INCREMENT ,
   order_no             bigint(20) DEFAULT NULL comment '订单号',
   user_id              int(11) DEFAULT NULL comment '用户id',
   shipping_id          int(11) DEFAULT null comment '收获地址id',
   payment              decimal(20,2) DEFAULT NULL comment '实际支付金额',
   payment_type         int(4) DEFAULT NULL comment '֧支付类型',
   postage              int(10) DEFAULT NULL comment '运费 单位元',
   status               int(4) DEFAULT NULL comment '订单状态:0-已取消 10-未支付 20-已付款 40-已发货 50-交易成功 60-交易关闭',
   payment_time         timestamp(0) DEFAULT NULL comment '支付时间',
   send_time            timestamp(0) DEFAULT NULL comment '发货时间',
   end_time             timestamp(0) DEFAULT NULL comment '交易完成时间',
   close_time           timestamp(0) DEFAULT NULL comment '交易关闭时间',
   create_time          timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   update_time          timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
   primary key (id),
   UNIQUE KEY `order_no_index` (order_no) USING BTREE
)ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=UTF8 COMMENT = '订单表';


/*==============================================================*/
/* Table: mall_order_item                                      */
/*==============================================================*/
create table mall_order_item
(
   id                   int(11) not null AUTO_INCREMENT,
   user_id              int(11) DEFAULT NULL,
   order_no             bigint(20) DEFAULT NULL comment '订单号',
   product_id           int(11) DEFAULT NULL comment '商品id',
   product_name         varchar(100) DEFAULT NULL comment '商品名称',
   product_image        varchar(500) DEFAULT NULL comment '商品图片',
   current_unit_price   decimal(20,2) DEFAULT null comment '生成订单时的价格',
   quantity             int(10) DEFAULT NULL comment '数量',
   total_price          decimal(20,2) DEFAULT NULL comment '总价格',
   create_time          timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   update_time          timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
   primary key (id),
   KEY `order_no_index` (order_no) USING BTREE,
   KEY `order_no_user_id_index` (order_no,user_id) USING BTREE
)ENGINE=InnoDB AUTO_INCREMENT=113 DEFAULT CHARSET=UTF8 COMMENT = '订单明细表';




/*==============================================================*/
/* Table: mall_product                                         */
/*==============================================================*/
create table mall_product
(
   id                   int(11) not null AUTO_INCREMENT comment '商品ID',
   category_id          int(11) not null comment '品类id',
   name                 varchar(50) not null comment '商品名称',
   subtitle             varchar(200) DEFAULT null comment '子标题',
   main_image           varchar(500) DEFAULT NULL comment '主图',
   sub_images           text comment '子图',
   detail               text comment '详情',
   price                decimal(20,2) not null comment '商品价格',
   stock                int(11) not null comment '库存',
   status               int(4) DEFAULT 1 comment '商品状态,1-在售 2-下架 3-删除',
   create_time          timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   update_time          timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
   primary key (id)
)ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=UTF8 COMMENT = '商品表';


/*==============================================================*/
/* Table: mall_shipping 收件地址表                               */
/*==============================================================*/
create table mall_shipping
(
   id                   int(11) not null AUTO_INCREMENT,
   user_id              int(11) DEFAULT NULL,
   receiver_name        varchar(20) DEFAULT NULL,
   receiver_phone       varchar(20) DEFAULT NULL,
   receiver_mobile      varchar(20) DEFAULT NULL,
   receiver_province    varchar(20) DEFAULT NULL,
   receiver_city        varchar(20) DEFAULT NULL,
   receiver_district    varchar(20) DEFAULT NULL,
   receiver_address     varchar(200) DEFAULT NULL,
   receiver_zip         varchar(6) DEFAULT NULL comment '邮编',
   create_time          timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   update_time          timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
   primary key (id)
)ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=UTF8 COMMENT = '收件地址表';


/*==============================================================*/
/* Table: mall_shipping 用户表                                   */
/*==============================================================*/
create table mall_user
(
   id                   int(11) not null AUTO_INCREMENT comment '用户id',
   username             varchar(50) not null comment '用户名称',
   password             varchar(50) not null comment '用户密码',
   email                varchar(50) DEFAULT NULL comment '邮箱',
   phone                varchar(20) DEFAULT NULL comment '手机号',
   role                 int(4) not null comment '角色',
   create_time          timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   update_time          timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
   primary key (id),
   UNIQUE KEY `user_name_unique` (username) USING BTREE
)ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=UTF8 COMMENT = '用户表';

