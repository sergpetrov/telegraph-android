package com.telex.base.presentation.settings.proxy

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.telex.base.R
import com.telex.base.extention.applySystemWindowInsetsPadding
import com.telex.base.model.source.local.ProxyServer
import com.telex.base.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_proxy_server.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
class ProxyServerFragment : BaseFragment(), ProxyServerView {

    override val layoutRes: Int = R.layout.fragment_proxy_server

    @InjectPresenter
    lateinit var presenter: ProxyServerPresenter

    @ProvidePresenter
    fun providePresenter(): ProxyServerPresenter {
        return scope.getInstance(ProxyServerPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootLayout.applySystemWindowInsetsPadding(applyTop = true, applyBottom = true)

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

        closeImageView.setOnClickListener { findNavController().popBackStack() }

        moreImageView.setOnClickListener {
            ProxyServerOptionsFragment().apply {
                deleteOption.onClick = { presenter.deleteProxyServer() }
            }.show(parentFragmentManager)
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
        findNavController().popBackStack()
    }
}
