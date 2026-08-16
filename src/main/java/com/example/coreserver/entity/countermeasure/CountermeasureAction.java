package com.example.coreserver.entity.countermeasure;

/**
 * 自动处置动作枚举。
 */
public enum CountermeasureAction {
    NO_ACTION, // 不操作
    UAV_ATTACK_AUTO, // 下发干扰指令
    DECEPTION_DRIVE, // 诱骗-驱离
    DECEPTION_CAPTURE, // 诱骗-捕获
    DECEPTION_DEFENSE, // 诱骗-金钟罩
    DECEPTION_INTERFERENCE, // 诱骗-干扰
    DECEPTION_NO_FLY // 诱骗-禁飞区
}
