package com.mruilab.evoplayer.beauty;

import com.faceunity.core.entity.FUBundleData;
import com.faceunity.core.model.facebeauty.FaceBeauty;
import com.faceunity.core.model.facebeauty.FaceBeautyFilterEnum;

public class FaceBeautySource {

    /**
     * 获取默认推荐美颜模型
     *
     * @return
     */
    public static FaceBeauty getDefaultFaceBeauty() {
        FaceBeauty recommendFaceBeauty = new FaceBeauty(new FUBundleData(DemoConfig.BUNDLE_FACE_BEAUTIFICATION));
        recommendFaceBeauty.setFilterName(FaceBeautyFilterEnum.ZIRAN_2);
        recommendFaceBeauty.setFilterIntensity(0.4);
        /*美肤*/
        recommendFaceBeauty.setSharpenIntensity(0.2); //0.2
        recommendFaceBeauty.setColorIntensity(0.3);  //0.3
        recommendFaceBeauty.setRedIntensity(0.3);   //0.3
        recommendFaceBeauty.setBlurIntensity(4.2); //4.2
        /*美型*/
        recommendFaceBeauty.setFaceShapeIntensity(1.0);
        recommendFaceBeauty.setEyeEnlargingIntensityV2(0.4);
        recommendFaceBeauty.setCheekVIntensity(0.5);
        recommendFaceBeauty.setNoseIntensityV2(0.5);
        recommendFaceBeauty.setForHeadIntensityV2(0.3);
        recommendFaceBeauty.setMouthIntensityV2(0.4);
        recommendFaceBeauty.setChinIntensity(0.3);
        return recommendFaceBeauty;
    }
}
