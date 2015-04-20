# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table category (
  id                        bigint not null,
  name                      varchar(255),
  picture                   varchar(255),
  constraint pk_category primary key (id))
;

create table comment (
  id                        bigint not null,
  comment                   varchar(255),
  user_id                   bigint,
  coupon_id                 bigint,
  date                      timestamp,
  constraint pk_comment primary key (id))
;

create table company (
  id                        bigint not null,
  email                     varchar(255),
  password                  varchar(255),
  adress                    varchar(255),
  city                      varchar(255),
  name                      varchar(255),
  created                   timestamp,
  updated                   timestamp,
  logo                      varchar(255),
  contact                   varchar(255),
  constraint pk_company primary key (id))
;

create table coupon (
  id                        bigint not null,
  name                      varchar(255),
  price                     double,
  created_at                timestamp,
  expired_at                timestamp,
  picture                   varchar(255),
  category_id               bigint,
  description               TEXT,
  remark                    varchar(255),
  seller_id                 bigint,
  min_order                 integer,
  max_order                 integer,
  usage                     timestamp,
  status                    integer,
  num_of_views              integer,
  constraint pk_coupon primary key (id))
;

create table email_verification (
  id                        varchar(255) not null,
  user_id                   bigint,
  created_on                timestamp,
  is_verified               boolean,
  constraint pk_email_verification primary key (id))
;

create table faq (
  id                        integer not null,
  question                  varchar(255),
  answer                    TEXT,
  constraint pk_faq primary key (id))
;

create table photo (
  id                        integer not null,
  path                      varchar(255),
  save_path                 varchar(255),
  coupon_id                 bigint,
  constraint pk_photo primary key (id))
;

create table pin (
  id                        integer not null,
  user_id                   bigint,
  code                      varchar(255),
  date                      timestamp,
  constraint pk_pin primary key (id))
;

create table question (
  id                        bigint not null,
  question                  varchar(255),
  user_id                   bigint,
  company_id                bigint,
  coupon_id                 bigint,
  answer                    varchar(255),
  question_date             timestamp,
  answer_date               timestamp,
  constraint pk_question primary key (id))
;

create table report (
  id                        bigint not null,
  message                   varchar(255),
  comment_id                bigint,
  user_id                   bigint,
  constraint pk_report primary key (id))
;

create table reset_pasword (
  id                        varchar(255) not null,
  user_email                varchar(255),
  date                      timestamp,
  constraint pk_reset_pasword primary key (id))
;

create table subscriber (
  id                        bigint not null,
  token                     varchar(255),
  subscriber_id             bigint,
  email                     varchar(255),
  constraint pk_subscriber primary key (id))
;

create table transaction_cp (
  id                        bigint not null,
  payment_id                varchar(255),
  coupon_price              double,
  quantity                  integer,
  total_price               double,
  token                     varchar(255),
  buyer_id                  bigint,
  coupon_id                 bigint,
  date                      timestamp,
  constraint pk_transaction_cp primary key (id))
;

create table user (
  id                        bigint not null,
  email                     varchar(255),
  password                  varchar(255),
  adress                    varchar(255),
  city                      varchar(255),
  username                  varchar(255),
  surname                   varchar(255),
  dob                       timestamp,
  gender                    varchar(255),
  is_admin                  boolean,
  created                   timestamp,
  updated                   timestamp,
  profile_picture           varchar(255),
  constraint pk_user primary key (id))
;

create sequence category_seq;

create sequence comment_seq;

create sequence company_seq;

create sequence coupon_seq;

create sequence email_verification_seq;

create sequence faq_seq;

create sequence photo_seq;

create sequence pin_seq;

create sequence question_seq;

create sequence report_seq;

create sequence reset_pasword_seq;

create sequence subscriber_seq;

create sequence transaction_cp_seq;

create sequence user_seq;

alter table comment add constraint fk_comment_user_1 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_comment_user_1 on comment (user_id);
alter table comment add constraint fk_comment_coupon_2 foreign key (coupon_id) references coupon (id) on delete restrict on update restrict;
create index ix_comment_coupon_2 on comment (coupon_id);
alter table coupon add constraint fk_coupon_category_3 foreign key (category_id) references category (id) on delete restrict on update restrict;
create index ix_coupon_category_3 on coupon (category_id);
alter table coupon add constraint fk_coupon_seller_4 foreign key (seller_id) references company (id) on delete restrict on update restrict;
create index ix_coupon_seller_4 on coupon (seller_id);
alter table photo add constraint fk_photo_coupon_5 foreign key (coupon_id) references coupon (id) on delete restrict on update restrict;
create index ix_photo_coupon_5 on photo (coupon_id);
alter table pin add constraint fk_pin_user_6 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_pin_user_6 on pin (user_id);
alter table question add constraint fk_question_user_7 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_question_user_7 on question (user_id);
alter table question add constraint fk_question_company_8 foreign key (company_id) references company (id) on delete restrict on update restrict;
create index ix_question_company_8 on question (company_id);
alter table question add constraint fk_question_coupon_9 foreign key (coupon_id) references coupon (id) on delete restrict on update restrict;
create index ix_question_coupon_9 on question (coupon_id);
alter table report add constraint fk_report_comment_10 foreign key (comment_id) references comment (id) on delete restrict on update restrict;
create index ix_report_comment_10 on report (comment_id);
alter table report add constraint fk_report_user_11 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_report_user_11 on report (user_id);
alter table subscriber add constraint fk_subscriber_subscriber_12 foreign key (subscriber_id) references user (id) on delete restrict on update restrict;
create index ix_subscriber_subscriber_12 on subscriber (subscriber_id);
alter table transaction_cp add constraint fk_transaction_cp_buyer_13 foreign key (buyer_id) references user (id) on delete restrict on update restrict;
create index ix_transaction_cp_buyer_13 on transaction_cp (buyer_id);
alter table transaction_cp add constraint fk_transaction_cp_coupon_14 foreign key (coupon_id) references coupon (id) on delete restrict on update restrict;
create index ix_transaction_cp_coupon_14 on transaction_cp (coupon_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists category;

drop table if exists comment;

drop table if exists company;

drop table if exists coupon;

drop table if exists email_verification;

drop table if exists faq;

drop table if exists photo;

drop table if exists pin;

drop table if exists question;

drop table if exists report;

drop table if exists reset_pasword;

drop table if exists subscriber;

drop table if exists transaction_cp;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists category_seq;

drop sequence if exists comment_seq;

drop sequence if exists company_seq;

drop sequence if exists coupon_seq;

drop sequence if exists email_verification_seq;

drop sequence if exists faq_seq;

drop sequence if exists photo_seq;

drop sequence if exists pin_seq;

drop sequence if exists question_seq;

drop sequence if exists report_seq;

drop sequence if exists reset_pasword_seq;

drop sequence if exists subscriber_seq;

drop sequence if exists transaction_cp_seq;

drop sequence if exists user_seq;

