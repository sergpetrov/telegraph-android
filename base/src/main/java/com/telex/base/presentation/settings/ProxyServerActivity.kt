package com.telex.base.presentation.settings

import android.os.Bundle
import com.telex.base.R
import com.telex.base.model.source.local.ProxyServer
import com.telex.base.presentation.base.BaseActivity
import kotlinx.android.synthetic.main.activity_proxy_server.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
class ProxyServerActivity : BaseActivity(), ProxyServerView {

    override val layoutRes: Int = R.layout.activity_proxy_server

    @InjectPresenter
    lateinit var presenter: ProxyServerPresenter

    @ProvidePresenter
    fun providePresenter(): ProxyServerPresenter {
        return scope.getInstance(ProxyServerPresenter::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupStatusBar()

        doneImageView.setOnClickListener {
            val type = when (typeRadioGroup.checkedRadioButtonId) {
                R.id.socks5RadioButton -> ProxyServer.Type.SOCKS
                R.id.httpRadioButton -> ProxyServer.Type.HTTP
                else -> throw IllegalArgumentException("unexpected proxy type")
            }

            presenter.saveProxyServer(
                    type = type,
                    host = serverEditText.text.toString().trim(),
                    port = portEditText.text.toString().trim(),
                    user = usernameEditText.text.toString(),
                    password = passwordEditText.text.toString()
            )
        }

        closeImageView.setOnClickListener { finish() }

        moreImageView.setOnClickListener {
            ProxyServerOptionsFragment().apply {
                deleteOption.onClick = { presenter.deleteProxyServer() }
                show(supportFragmentManager)
            }
        }
    }

    override fun showInvalidServerError() {
        serverTextInputLayout.error = getString(R.string.invalid)
    }

    override fun hideInvalidServerError() {
        serverTextInputLayout.isErrorEnabled = false
    }

    override fun showInvalidPortError() {
        portTextInputLayout.error = getString(R.string.invalid)
    }

    override fun hideInvalidPortError() {
        portTextInputLayout.isErrorEnabled = false
    }

    override fun showProxyServer(proxy: ProxyServer) {
        val typeRadioButtonId = when (proxy.type) {
            ProxyServer.Type.SOCKS -> R.id.socks5RadioButton
            ProxyServer.Type.HTTP -> R.id.httpRadioButton
        }

        typeRadioGroup.check(typeRadioButtonId)
        serverEditText.setText(proxy.host)
        portEditText.setText(proxy.port.toString())
        usernameEditText.setText(proxy.user)
        passwordEditText.setText(proxy.password)
    }

    override fun onProxyServerSaved() {
        finish()
    }
}
