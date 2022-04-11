//
// Created by mruilab on 2022/4/8.
//

#ifndef EVOPLAYER_DEF_DRAWER_PROXY_IMPL_H
#define EVOPLAYER_DEF_DRAWER_PROXY_IMPL_H

#include "drawer_proxy.h"
#include <vector>

class DefDrawerProxyImpl : public DrawerProxy {

private:
    std::vector<Drawer *> m_drawers;

public:
    void AddDrawer(Drawer *drawer);

    void SetDisplaySize(int width, int height) override;

    void Draw() override;

    void Release() override;
};


#endif //EVOPLAYER_DEF_DRAWER_PROXY_IMPL_H
