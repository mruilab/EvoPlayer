//
// Created by mruilab on 2022/4/8.
//

#ifndef EVOPLAYER_DRAWER_PROXY_H
#define EVOPLAYER_DRAWER_PROXY_H

#include "drawer.h"

class DrawerProxy {
public:
    virtual void AddDrawer(Drawer *drawer) = 0;

    virtual void SetDisplaySize(int width, int height) = 0;

    virtual void Draw() = 0;

    virtual void Release() = 0;

    virtual ~DrawerProxy() {}
};


#endif //EVOPLAYER_DRAWER_PROXY_H
