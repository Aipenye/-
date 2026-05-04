
import VueRouter from 'vue-router';

const routes = [
    {
        path: '/',
        name: 'login',
        component: () => import('../components/Login')
    },
    {
        path: '/Index',
        name: 'index',
        component: () => import('../components/Index'),
        children: [
            {
                path: '/Home',
                name: 'home',
                meta: { title: '首页' },
                component: () => import('../components/Home')
            },
            {
                path: '/Profile',
                name: 'profile',
                meta: { title: '个人中心' },
                component: () => import('../components/user/UserProfile')
            },
            {
                path: '/AgvSim',
                name: 'agvSim',
                meta: { title: 'AGV仿真' },
                component: () => import('../components/agv/AgvSimulation')
            }
        ]
    },
    {
        path: '/Worker',
        name: 'worker',
        component: () => import('../components/worker/WorkerIndex'),
        children: [
            {
                path: '/Worker/orders',
                name: 'workerOrders',
                component: () => import('../components/worker/WorkerOrders')
            }
        ]
    }
]

const router = new VueRouter({
    mode: 'history',
    routes
})

export function resetRouter() {
    router.matcher = new VueRouter({
        mode: 'history',
        routes: []
    }).matcher
}

const VueRouterPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(to) {
    return VueRouterPush.call(this, to).catch(err => err)
}

export default router;
