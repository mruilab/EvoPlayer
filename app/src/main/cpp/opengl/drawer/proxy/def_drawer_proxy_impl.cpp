//
// 默认画面渲染代理
// Created by mruilab on 2022/4/8.
//

#include "def_drawer_proxy_impl.h"

void DefDrawerProxyImpl::AddDrawer(Drawer *drawer) {
    m_drawers.push_back(drawer);
}

void DefDrawerProxyImpl::SetDisplaySize(int width, int height) {
    for (int i = 0; i < m_drawers.size(); ++i) {
        m_drawers[i]->SetDisplaySize(width, height);
    }
}

void DefDrawerProxyImpl::Draw() {
    for (int i = 0; i < m_drawers.size(); ++i) {
        m_drawers[i]->Draw();
    }
}

void DefDrawerProxyImpl::Release() {
    for (int i = 0; i < m_drawers.size(); ++i) {
        m_drawers[i]->Release();
        delete m_drawers[i];
    }

    m_drawers.clear();
}